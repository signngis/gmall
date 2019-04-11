package com.atguigu.gmall0228.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class MyFdfsUploadUtil {

    public static String uploadImage(MultipartFile file){

        String httpPath = "http://192.168.85.134";

        // 配置fastdfs全局变量，读取一个配置文件
        // fastdfs的tracker服务地址信息，和过期时间等其他配置
        String conf = MyFdfsUploadUtil.class.getClassLoader().getResource("tracker.conf").getFile();
        try {
            ClientGlobal.init(conf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 获得一个tracker服务
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获得一个storage服务
        StorageClient storageClient = new StorageClient(connection, null);


        // 使用storage服务进行上传操作
        String originalFilename = file.getOriginalFilename();
        String[] split = originalFilename.split("\\.");
        String extName = split[split.length - 1];
        try {
            String[] paths = storageClient.upload_file(file.getBytes(), extName, null);

            // 解析路径
            for (String path : paths) {
                httpPath = httpPath+"/" + path;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        return httpPath;
    }
}
