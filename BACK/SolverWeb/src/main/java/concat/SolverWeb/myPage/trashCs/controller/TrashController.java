package concat.SolverWeb.myPage.trashCs.controller;

import concat.SolverWeb.myPage.trashCs.service.TrashService;
import concat.SolverWeb.user.yoonseo.dto.UserDTO;
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

    @Autowired
    private TrashService trashService;

    @GetMapping("/trash")
    public Map<String, Object> moveToTrash(
            @RequestParam("videoUrl") String videoUrl,
            @RequestParam("userId") String userId) {

        try {
            if (videoUrl == null || userId == null) {
                throw new IllegalArgumentException("VideoUrl and userId cannot be null");
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(userId);

            if (userDTO.getUserId() == null) {
                throw new IllegalArgumentException("UserId cannot be null");
            }

            boolean success = trashService.moveToTrash(videoUrl, userDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);

            if (success) {
                logger.info("Moved to trash successfully");
            } else {
                logger.warn("Failed to move to trash");
            }
            return response;
        } catch (Exception e) {
            logger.error("Error processing move to trash request", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}
