package application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildRequest {
    private Integer what;
    private String name;
    private List<List<String>> extension;
    private List<List<String>> lexic;
    private List<List<String>> syntax;
    private String template;
}
