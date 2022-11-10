package com.wks.caseengine.rest.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.wks.caseengine.data.iimport.DataImportService;

//TODO move this out of the Rest API module - better to be independent
@Component
public class DataImportCommandRunner implements CommandLineRunner {

	@Autowired
	private DataImportService dataImportService;

	@Value("${data.import.folder}")
	private String dataImportFolder;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Teste: " + dataImportFolder);
		if (dataImportFolder != null && !dataImportFolder.isEmpty()) {

			Gson gson = new Gson();

			listFiles(dataImportFolder).forEach(fileName -> {
				JsonReader reader;
				try {
					reader = new JsonReader(new FileReader(fileName));
					dataImportService.importData(gson.fromJson(reader, JsonObject.class));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
		}

	}

	public Set<String> listFiles(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::toAbsolutePath).map(Path::toString)
					.collect(Collectors.toSet());
		}
	}

}
