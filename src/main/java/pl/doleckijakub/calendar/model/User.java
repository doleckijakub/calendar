package pl.doleckijakub.calendar.model;

import jakarta.validation.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public record User(@Id UUID id, @NotBlank String username) {

}
