package no.systema.jservices.tvinn.digitoll.external.house;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class TesterAttach {

	public static void main(String[] args) {
		//XX1234455-SYS078566_xx999316751_xx3IOSTDS
		// TODO Auto-generated method stub
		FileAttachmentService service = new FileAttachmentService("/Users/oscardelatorre/tmp/");
		List <Path> x = service.getFileAttachments("XX1234455-SYS078566", "999316751");
		for (Path file : x) {
			System.out.println(file.getFileName().toString());
				try {
	            String payloadPath = service.getAttachmentsPath() + File.separator + file.getFileName().toString();
	            String base64String = service.convertImageToBase64(payloadPath);
	            System.out.println("Base64 String: " + base64String);
	            //decode and write
	            /*byte[] bytes = Base64.getDecoder().decode(base64String);
	            Path filePath = Paths.get("/Users/oscardelatorre/" + File.separator + "ny-" + payload);
	            Files.write(filePath, bytes);
	            */
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
}
