package com.imooc.bilibili;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileApi {
    @Autowired
    private FileService fileService;
    @PostMapping("/md5File")
    public JsonResponse<String> getFileMF5(MultipartFile file) throws IOException {
      String  filemd5=  fileService.getFileMd5(file);
      return new JsonResponse<>(filemd5);
    }
    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileSlices(MultipartFile file,
                                                 String filemd5,
                                                 Integer sliceNo,
                                                 Integer totalSlice) throws Exception {
        String filePath = fileService.uploadFileBySlices(file, filemd5, sliceNo, totalSlice);
        return new JsonResponse<>(filePath);
    }
}
