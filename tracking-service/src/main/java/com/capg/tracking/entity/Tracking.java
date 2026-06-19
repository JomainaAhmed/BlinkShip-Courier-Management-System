package com.capg.tracking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tracking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long deliveryId;

	private String status;

	private String location;

	private LocalDateTime timestamp;

	@Column(columnDefinition = "BYTEA")
	private byte[] document;

	private String fileName;
	private String fileType;

	@PrePersist
	public void onCreate() {
		this.timestamp = LocalDateTime.now();
	}
}