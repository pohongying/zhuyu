package com.luoyi.implatform.fastdfs.web;

import com.luoyi.implatform.fastdfs.client.*;
import com.luoyi.implatform.fastdfs.config.FastDFSProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * 文件接口
 * <p>
 *
 * @author jiangzhou.bo@hand-china.com
 * @version 1.0
 * @name FileObjectController
 * @date 2017-10-15 14:09
 */
@RestController
@RequestMapping("/fastdfs")
public class FileObjectController {

    private static final Logger logger = LoggerFactory.getLogger(FileObjectController.class);
    @Resource
    private FastDFSUtil fastDFSUtil;

    @Resource
    private FastDFSProperties fastDFSProperties;


    @GetMapping("/test")
    public FileResponseData test() {
        return new FileResponseData(true);
    }

    /**
     * 上传文件通用，只上传文件到服务器，不会保存记录到数据库
     *
     * @param file
     * @param request
     * @return 返回文件路径等信息
     */
    @PostMapping("/upload/file")
    public FileResponseData uploadFileSample(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return uploadSample(file, request);
    }

    /**
     * 只能上传图片，只上传文件到服务器，不会保存记录到数据库. <br>
     * 会检查文件格式是否正确，默认只能上传 ['png', 'gif', 'jpeg', 'jpg'] 几种类型.
     *
     * @param file
     * @param request
     * @return 返回文件路径等信息
     */
    @PostMapping("/upload/image")
    public FileResponseData uploadImageSample(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 检查文件类型
        if (!FileCheck.checkImage(file.getOriginalFilename())) {
            FileResponseData responseData = new FileResponseData(false);
            responseData.setCode(ErrorCode.FILE_TYPE_ERROR_IMAGE.CODE);
            responseData.setMessage(ErrorCode.FILE_TYPE_ERROR_IMAGE.MESSAGE);
            return responseData;
        }

        return uploadSample(file, request);
    }

    /**
     * 只能上传文档，只上传文件到服务器，不会保存记录到数据库. <br>
     * 会检查文件格式是否正确，默认只能上传 ['pdf', 'ppt', 'xls', 'xlsx', 'pptx', 'doc', 'docx'] 几种类型.
     *
     * @param file
     * @param request
     * @return 返回文件路径等信息
     */
    @PostMapping("/upload/doc")
    public FileResponseData uploadDocSample(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 检查文件类型
        if (!FileCheck.checkDoc(file.getOriginalFilename())) {
            FileResponseData responseData = new FileResponseData(false);
            responseData.setCode(ErrorCode.FILE_TYPE_ERROR_DOC.CODE);
            responseData.setMessage(ErrorCode.FILE_TYPE_ERROR_DOC.MESSAGE);
            return responseData;
        }

        return uploadSample(file, request);
    }

    /**
     * 以附件形式下载文件
     *
     * @param filePath 文件地址
     * @param response
     */
    @GetMapping("/file")
    public void downloadFile(@RequestParam("filePath") String filePath, HttpServletResponse response) throws FastDFSException, IOException {
        try {
            fastDFSUtil.downloadFile(filePath, (OutputStream) response);
        } catch (FastDFSException | IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取图片 使用输出流输出字节码，可以使用< img>标签显示图片<br>
     *
     * @param filePath 图片地址
     * @param response
     */
    @GetMapping("/image")
    public void downloadImage(@RequestParam("filePath") String filePath, HttpServletResponse response) throws FastDFSException {
        try {
            fastDFSUtil.downloadFile(filePath, response.getOutputStream());
        } catch (FastDFSException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据指定的路径删除服务器文件，适用于没有保存数据库记录的文件
     *
     * @param filePath
     */
    @DeleteMapping("/file")
    public FileResponseData deleteFile(@RequestParam("filePath") String filePath, Locale locale) throws Exception {
        FileResponseData responseData = new FileResponseData();
        try {
            fastDFSUtil.deleteFile(filePath);
        } catch (FastDFSException e) {
            e.printStackTrace();
            responseData.setSuccess(false);
            responseData.setCode(e.getCode());
            responseData.setMessage(e.getMessage());
        }
        return responseData;
    }

    /**
     * 获取访问文件的token
     *
     * @param filePath 文件路径
     * @return
     */
    @GetMapping("/token")
    public FileResponseData getToken(@RequestParam("filePath") String filePath) {
        FileResponseData responseData = new FileResponseData();
        String fastDFSHttpSecretKey = fastDFSProperties.getHttpSecretKey();
        // 设置访文件的Http地址. 有时效性.
        String token = FastDFSUtil.getToken(filePath, fastDFSHttpSecretKey);
        responseData.setToken(token);
        String fileServerAddr = fastDFSProperties.getFileServerAddr();
        responseData.setHttpUrl(fileServerAddr + "/" + filePath + "?" + token);

        return responseData;
    }

    /**
     * 上传通用方法，只上传到服务器，不保存记录到数据库
     *
     * @param file
     * @param request
     * @return
     */
    public FileResponseData uploadSample(MultipartFile file, HttpServletRequest request) {
        FileResponseData responseData = new FileResponseData();
        try {
            // 上传到服务器
            String filepath = fastDFSUtil.uploadFileWithMultipart(file);
            responseData.setCode("200");
            responseData.setMessage("上传成功");
            responseData.setSuccess(true);
            responseData.setFileName(file.getOriginalFilename());
            responseData.setFilePath(filepath);
            responseData.setFileType(FastDFSUtil.getFilenameSuffix(file.getOriginalFilename()));

            // 设置访文件的Http地址. 有时效性.
//            String token = FastDFSClient.getToken(filepath, fastDFSHttpSecretKey);
//            responseData.setToken(token);
//            responseData.setHttpUrl(fileServerAddr + "/" + filepath + "?" + token);

            String fileServerAddr = fastDFSProperties.getFileServerAddr();
            responseData.setHttpUrl(fileServerAddr + "/" + filepath);
            // 打印相关信息
            logger.info("文件上传成功: 文件名={}, 文件路径={}, 文件类型={}, 访问URL={}",
                    file.getOriginalFilename(), filepath, FastDFSUtil.getFilenameSuffix(file.getOriginalFilename()), responseData.getHttpUrl());
        } catch (FastDFSException e) {
            responseData.setSuccess(false);
            responseData.setCode(e.getCode());
            responseData.setMessage(e.getMessage());
        }

        return responseData;
    }

}

