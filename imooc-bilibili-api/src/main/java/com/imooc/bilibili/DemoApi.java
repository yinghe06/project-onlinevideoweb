package com.imooc.bilibili;

import com.imooc.bilibili.domain.FollowingGroup;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.Video;
import com.imooc.bilibili.service.DemoService;
import com.imooc.bilibili.service.FileService;
import com.imooc.bilibili.service.util.FastdfsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.ConfigurationException;
import java.io.IOException;

@RestController
public class DemoApi {
    @Autowired
    private DemoService demoService;
    @Autowired
    private FastdfsUtil fastdfsUtil;
    @Autowired
    private FileService fileService;
    @GetMapping("/query")
    public int query(@RequestParam int id) {
        return  demoService.query(id);
    }
    @GetMapping("/slices")
    private void slices(MultipartFile file) throws ConfigurationException, IOException {
        fastdfsUtil.convertFileToSlices(file);
    }
    @PostMapping("/file-slices")
    private JsonResponse<String> uploadFileBySlice(MultipartFile multipartFile,
                                                   String fileMd5,
                                                   Integer SliceNo,
                                                   Integer totalSliceNo) throws Exception {

        String path = fileService.uploadFileBySlices(multipartFile, fileMd5, SliceNo, totalSliceNo);
        return  new JsonResponse<>(path);

    }


}
