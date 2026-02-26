package ua.trinity.iwk.backend.tax.jurisdictions.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Jurisdiction {

    @Id
    private String id;
    private String name;
    private String code;

    private Breakdown breakdown;
}
