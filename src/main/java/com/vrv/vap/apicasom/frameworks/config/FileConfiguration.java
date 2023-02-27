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
	/**
	 * 文件路径
	 */
	private String filePath;
	/**
	 * 导出模板路径
	 */
	private String templatePath;
}
