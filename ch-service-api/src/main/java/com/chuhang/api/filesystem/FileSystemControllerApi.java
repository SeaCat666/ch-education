package com.chuhang.api.filesystem;

import com.chuhang.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件管理接口",description = "文件管理接口，提供页面的增、删、改、查")
public interface FileSystemControllerApi {

    @ApiOperation("上传文件接口")
    UploadFileResult upload(MultipartFile multipartFile,
                            String filetag,
                            String businesskey,
                            String metadata);
}