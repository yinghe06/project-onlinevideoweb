package com.imooc.bilibili.service.util;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;

import com.imooc.bilibili.domain.exception.ConditionException;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sun.text.resources.fi.JavaTimeSupplementary_fi;

import javax.naming.ConfigurationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
@Component
public class FastdfsUtil {
    @Autowired
    FastFileStorageClient fastFileStorageClient;
    @Autowired
    AppendFileStorageClient appendFileStorageClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String PATH_KEY = "path-key:";

    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";

    private static final String UPLOADED_NO_KEY = "uploaded-no-key:";

    private static final String DEFAULT_GROUP = "group1";
    private static final int SLICE_SIZE = 1024 * 1024 * 2;
    public String uploadCommonFile(MultipartFile file) throws IOException, ConfigurationException {
        HashSet<MetaData> metadataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metadataSet);
        return storePath.getPath();
    }
    public void delete(String filePath){
        fastFileStorageClient.deleteFile(filePath);
    }
    public String getFileType(MultipartFile file)  {
        if(file == null){
            throw new ConditionException("非法文件！");
        }
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index+1);
    }
    public String uploadAppenderFile(MultipartFile file) throws ConfigurationException, IOException {
        //String originalFilename = file.getOriginalFilename();
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }
    public void modifyAppenderFile(MultipartFile file,String filePath,long offset) throws IOException {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }
    public String uploadFileBySlices(MultipartFile file,String md5,Integer sliceNo,Integer totalslice) throws ConfigurationException, IOException {
        if (file==null||sliceNo==null ||totalslice==null){
            throw  new ConditionException("参数异常");
        }
        String pathKey=PATH_KEY+md5;
        String uploadedSizeKey=UPLOADED_SIZE_KEY+md5;
        String uploadedNoKey=UPLOADED_NO_KEY+md5;
        String uploadedSizeKeyStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize=0L;
        if(!StringUtils.isNullOrEmpty(uploadedSizeKeyStr)){
            uploadedSize=Long.valueOf(uploadedSizeKeyStr);

        }
        String fileType=this.getFileType(file);
        if (sliceNo==1){
            String path = this.uploadAppenderFile(file);
            if(StringUtils.isNullOrEmpty(path)){
                throw new ConditionException("上传失败");
            }
            redisTemplate.opsForValue().set(pathKey, path);

            redisTemplate.opsForValue().set(uploadedNoKey,"1");

        }else {
            String path = redisTemplate.opsForValue().get(pathKey);
            if (StringUtils.isNullOrEmpty(path)){
                throw  new ConfigurationException("上传失败");

            }
            this.modifyAppenderFile(file, path,uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        uploadedSize+=file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey,String.valueOf(uploadedSize));
        //如果分片全部上传完成，清空redis中相关keyvalue
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath="";
        if (uploadedNo.equals(totalslice)){
            resultPath=redisTemplate.opsForValue().get(pathKey);
            List<String> KeyList = Arrays.asList(uploadedNoKey, uploadedSizeKey, pathKey);
            redisTemplate.delete(KeyList);
        }
        return resultPath;
    }
    public void convertFileToSlices(MultipartFile multipartFile) throws IOException, ConfigurationException {
        System.out.println(multipartFile);
        String fileType = this.getFileType(multipartFile);
        File file = this.multiFileToFile(multipartFile);
        long fileLength= file.length();
        int count=1;
        for (int i=0;i<fileLength;i+=SLICE_SIZE){
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(i);
            byte[] bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read(bytes);
            String path="D:/Users/po3a/tmpfile"+count+"."+fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes,0,len);
            fos.close();
            randomAccessFile.close();
            count++;

        }
        file.delete();


    }
    public File multiFileToFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String[] filename = originalFilename.split("\\.");
        File file = File.createTempFile(filename[0], "." + filename[1]);
        multipartFile.transferTo(file);
        return file;
    }
    @Value("${fdfs.http.storage-addr}")
    private String httpFastdfsStorageAddr;
    public void viewVideoOnlineByslices(HttpServletRequest request, HttpServletResponse response, String path)throws  Exception {
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long fileSize = fileInfo.getFileSize();
        String url=httpFastdfsStorageAddr+path;
        //M00/00/00/CiBckGZ1ihiAAvdKAA1WZCX5dJs421.mp4
        Enumeration<String> headerNames = request.getHeaderNames();
        HashMap<String, Object> headers = new HashMap<>();
        while (headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        String rangeStr = request.getHeader("Range");
        String[] range;
        if (StringUtils.isNullOrEmpty(rangeStr)){
            rangeStr="bytes=0-"+(fileSize-1);
        }
        range=rangeStr.split("bytes=|-");
        long begin=0;
        long end=fileSize-1;
        if (range.length>=2){
            begin=Long.parseLong(range[1]);
        }
        if (range.length>=3){
            end=Long.parseLong(range[2]);
        }
        long len=end-begin+1;
        String contentRange="bytes"+begin+"-"+end+"/"+fileSize;
        response.setHeader("Content-Range",contentRange);
        response.setHeader("Accept_Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int) len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        HttpUtil.get(url, headers, response);
    }
}
