package com.chuhang.filesystem.controller;

import com.chuhang.api.filesystem.FileSystemControllerApi;
import com.chuhang.filesystem.service.FileSystemService;
import com.chuhang.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/filesystem")
public class FileSystemController implements FileSystemControllerApi {
    @Autowired
    FileSystemService fileSystemService;

    @Override
    @PostMapping("/upload")
    public UploadFileResult upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "filetag", required = true) String
                                           filetag,
                                   @RequestParam(value = "businesskey", required = false) String
                                           businesskey,
                                   @RequestParam(value = "metedata", required = false) String
                                           metadata) {
        return fileSystemService.upload(file,filetag,businesskey,metadata);
    }
}
