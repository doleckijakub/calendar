package pl.doleckijakub.calendar.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
public record Event(@Id UUID id, @NotBlank String title, String description, @NotNull Timestamp start_time, Timestamp end_time, @NotNull UUID author_id, @NotNull String color) {

}
