package application.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Tune {
    @NotBlank
    private String language;
    @NotBlank
    private String scanner;
    @NotBlank
    private String parser;
    //@NotBlank
    private String saving;
    @NotNull
    private int deltat;
    @NotNull
    private int assist;
}
