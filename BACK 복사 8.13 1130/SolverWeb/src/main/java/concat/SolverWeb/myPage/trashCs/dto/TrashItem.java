package concat.SolverWeb.myPage.trashCs.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TrashItem {
    private String title;
    private String imageUrl;
    private Instant lastModified;

    public TrashItem() {}

    public TrashItem(String title, String imageUrl, Instant lastModified) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.lastModified = lastModified;
    }
}
