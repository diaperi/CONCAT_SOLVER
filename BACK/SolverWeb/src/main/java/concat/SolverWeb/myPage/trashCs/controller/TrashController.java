package concat.SolverWeb.myPage.trashCs.controller;

import concat.SolverWeb.myPage.trashCs.service.TrashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/yuna")
public class TrashController {

    private static final Logger logger = LoggerFactory.getLogger(TrashController.class);

    private final TrashService trashService;

    @Autowired
    public TrashController(TrashService trashService) {
        this.trashService = trashService;
    }

    @PostMapping("/trash")
    public Map<String, Object> moveToTrash(@RequestBody Map<String, String> request) {
        String videoUrl = request.get("videoUrl");
        boolean success = trashService.moveToTrash(videoUrl);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);

        if (success) {
            logger.info("Moved to trash successfully");
        } else {
            logger.warn("Failed to move to trash");
        }
        return response;
    }
}
