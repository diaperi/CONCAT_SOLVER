package concat.SolverWeb.myPage.trashCs.controller;

import concat.SolverWeb.myPage.trashCs.dto.TrashItem;
import concat.SolverWeb.myPage.trashCs.service.S3CleanupService;
import concat.SolverWeb.myPage.trashCs.service.TrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/myPage")
public class TrashCsController {

    private final TrashService trashService;
    private final S3CleanupService s3CleanupService;

    @Autowired
    public TrashCsController(TrashService trashService, S3CleanupService s3CleanupService) {
        this.trashService = trashService;
        this.s3CleanupService = s3CleanupService;
    }

    @GetMapping("/cs")
    public String cs() {
        return "yuna/cs";
    }

    @GetMapping("/trash")
    public String getTrashItems(Model model) {
        List<TrashItem> todayItems = trashService.getItemsForToday();
        List<TrashItem> thisWeekItems = trashService.getItemsForThisWeek();
        List<TrashItem> lastWeekItems = trashService.getItemsForLastWeek();
        List<TrashItem> thisMonthItems = trashService.getItemsForThisMonth();

        model.addAttribute("todayItems", todayItems);
        model.addAttribute("thisWeekItems", thisWeekItems);
        model.addAttribute("lastWeekItems", lastWeekItems);
        model.addAttribute("thisMonthItems", thisMonthItems);

        return "yuna/trash";
    }

    @GetMapping("/moveToTrash")
    public String moveToTrash(@RequestParam String fileName) {
        String commonTimestamp = TrashService.extractCommonTimestamp(fileName);
        if (commonTimestamp != null) {
            trashService.moveFilesToTrash(commonTimestamp);
        }
        return "redirect:/myPage/trash";
    }

    @PostMapping("/cleanupTrash")
    public ResponseEntity<?> cleanupTrash() {
        try {
            s3CleanupService.deleteOldFiles();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete trash");
        }
    }

    @PostMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@RequestParam String fileName) {
        try {
            trashService.deleteFile(fileName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }
    }
}
