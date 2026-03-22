package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@RestController
@Slf4j
@RequestMapping("admin/common")
@Api(tags = "通用接口")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("图片上传")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("正在上传文件{}",file);
        try {
            String originNameFile = file.getOriginalFilename();
            String extension = originNameFile.substring(originNameFile.lastIndexOf('.'));
            String objectName = UUID.randomUUID().toString();
            String filePath = objectName+extension;
            aliOssUtil.upload(file.getBytes(),filePath);
            return Result.success(filePath);
        } catch (IOException e) {
           log.error("文件上传失败{}",e);
        }
       return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
