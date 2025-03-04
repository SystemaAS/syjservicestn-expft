package no.systema.jservices.tvinn.digitoll.external.house;

import org.springframework.stereotype.Service;

import no.systema.jservices.tvinn.digitoll.external.house.dao.MessageOutbound;

@Service
public interface XmlWriterInterface {
	public int writeFileOnDisk(MessageOutbound msg, String jsonPayload);

}
