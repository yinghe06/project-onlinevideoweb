package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.FileDao;
import com.imooc.bilibili.domain.File;

import com.imooc.bilibili.service.util.FastdfsUtil;
import com.imooc.bilibili.service.util.MD5Util;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class FileService {

    @Autowired
    private FileDao fileDao;

    @Autowired
    private FastdfsUtil fastDFSUtil;
    public String uploadFileBySlices(MultipartFile slice,
                                     String fileMD5,
                                     Integer sliceNo,
                                     Integer totalSliceNo) throws Exception {
        File dbFileMD5 = fileDao.getFileByMD5(fileMD5);
        if(dbFileMD5 != null){
            return dbFileMD5.getUrl();
        }
        String url = fastDFSUtil.uploadFileBySlices(slice, fileMD5, sliceNo, totalSliceNo);
        if(!StringUtil.isNullOrEmpty(url)){
            dbFileMD5 = new File();
            dbFileMD5.setCreateTime(new Date());
            dbFileMD5.setMd5(fileMD5);
            dbFileMD5.setUrl(url);
            dbFileMD5.setType(fastDFSUtil.getFileType(slice));
            fileDao.addFile(dbFileMD5);
        }
        return url;
    }

    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

    public File getFileByMd5(String fileMd5) {
        return fileDao.getFileByMD5(fileMd5);
    }

    public String getFileMd5(MultipartFile file) throws IOException {
        InputStream fis = file.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len=fis.read(buffer))>0){
            baos.write(buffer, 0, len);
        }
        fis.close();
        return DigestUtils.md5Hex(baos.toByteArray());
    }
}
