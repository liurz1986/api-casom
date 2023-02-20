package com.vrv.vap.apicasom.frameworks.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * FileConfiguration
 * @author liurz
 *
 */

@Configuration
@Data
@ConfigurationProperties(prefix="file")
public class FileConfiguration {

	private String filePath; //文件路径
}
