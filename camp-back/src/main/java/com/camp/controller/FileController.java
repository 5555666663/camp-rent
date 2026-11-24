package com.camp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.camp.exception.BusinessException;
import com.camp.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 通用文件上传（定损照片等）
 *
 * @author camp
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Set<String> ALLOW = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));

    @Value("${file.upload-dir:D:/DSH/camp-rent/uploads}")
    private String uploadDir;

    /** 上传图片，返回可访问地址 */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件为空");
        }
        String original = file.getOriginalFilename();
        String ext = original == null ? "" : FileUtil.extName(original).toLowerCase();
        if (!ALLOW.contains(ext)) {
            throw new BusinessException("仅支持 jpg / jpeg / png 图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("图片不能超过 5MB");
        }
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        File dir = new File(uploadDir, month);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }
        String name = IdUtil.fastSimpleUUID() + "." + ext;
        File target = new File(dir, name);
        try {
            file.transferTo(target);
        } catch (Exception e) {
            throw new BusinessException("保存文件失败：" + e.getMessage());
        }
        return Result.success("/uploads/" + month + "/" + name);
    }
}
