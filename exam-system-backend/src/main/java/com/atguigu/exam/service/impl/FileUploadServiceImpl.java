package com.atguigu.exam.service.impl;

import com.atguigu.exam.config.properties.MinioProperties;
import com.atguigu.exam.service.FileUploadService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.UUID.randomUUID;

/**
 * projectName: com.atguigu.exam.service.impl
 *
 * @author: 赵伟风
 * description:
 */
@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties minioProperties;
    @Override
    public String upload(String folder, MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .build());

        if(!bucketExists){
            String policy = """
                          {
                                "Statement" : [ {
                                  "Action" : "s3:GetObject",
                                  "Effect" : "Allow",
                                  "Principal" : "*",
                                  "Resource" : "arn:aws:s3:::%s/*"
                                } ],
                                "Version" : "2012-10-17"
                          }
                      """.formatted(minioProperties.getBucketName());
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .config(policy)
                    .build());
        }
        //UUID
        String objectName = folder + "/"
                + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/"
                + randomUUID().toString().replaceAll("-","") + "_"
                + file.getOriginalFilename();
        minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .contentType(file.getContentType())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build());

        String url = String.join("/",minioProperties.getEndpoint(),minioProperties.getBucketName(),objectName);
        log.info("{}上传成功：回显地址为{}",objectName,url);
        return url;
    }
}
