package application.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class RuleDocument implements Serializable {

    @NotBlank(message = "Rules is mandatory")
    private String rules;

    @NotNull(message = "Data is mandatory")
    @Valid
    private RuleData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleData {
        @JsonProperty("rList")
        private List<RuleItem> rList;
        private String extension;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleItem {
        @JsonProperty("lPart")
        private String lPart;
        @JsonProperty("rPart")
        private List<String> rPart;
    }
}

