package com.tms.restapi.toolsmanagement.trainer.service;

import com.tms.restapi.toolsmanagement.admin.dto.ActivityDto;
import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnItem;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRepository;
import com.tms.restapi.toolsmanagement.issuance.repository.ReturnRepository;
import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainerDashboardService {

    @Autowired
    private IssuanceRepository issuanceRepository;

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    public AdminDashboardResponse getDashboardForTrainer(Long trainerId) {
        AdminDashboardResponse resp = new AdminDashboardResponse();
        if (trainerId == null) return resp;

        LocalDate today = LocalDate.now();

        List<Issuance> issuances = issuanceRepository.findByTrainerId(trainerId);
        List<ReturnRecord> returns = returnRepository.findByIssuance_TrainerId(trainerId);

        int totalIssuance = issuances == null ? 0 : issuances.size();
        int totalReturns = returns == null ? 0 : returns.size();

        int issuanceToday = 0;
        int overdue = 0;
        if (issuances != null) {
            for (Issuance i : issuances) {
                if (i.getIssuanceDate() != null && i.getIssuanceDate().isEqual(today)) issuanceToday++;
                if (i.getStatus() != null && i.getStatus().equalsIgnoreCase("OVERDUE")) overdue++;
            }
        }

        resp.setTotalTools(0); // not applicable for trainer view
        resp.setTotalKits(0);
        resp.setIssuanceToday(issuanceToday);
        resp.setReturnsToday(returns == null ? 0 : (int) returns.stream().filter(r -> r.getActualReturnDate() != null && r.getActualReturnDate().isEqual(today)).count());
        resp.setOverdueIssuance(overdue);
        resp.setDamagedCount((int) (returns == null ? 0 : returns.stream().flatMap(r -> r.getItems() == null ? List.<ReturnItem>of().stream() : r.getItems().stream())
                .filter(ri -> ri.getCondition() != null && (ri.getCondition().equalsIgnoreCase("damaged") || ri.getCondition().equalsIgnoreCase("missing") || ri.getCondition().equalsIgnoreCase("obsolete"))).count()));

        // recent activities: issuance and returns by this trainer
        List<ActivityDto> activities = new ArrayList<>();

        if (issuances != null) {
            for (Issuance i : issuances) {
                String items = buildItemList(i.getToolIds(), i.getKitIds());
                ActivityDto act = new ActivityDto("Tool Issued", i.getTrainerName(), i.getToolIds() != null && !i.getToolIds().isEmpty() ? "Tool" : "Kit", items, i.getIssuanceDate(), i.getLocation());
                LocalDateTime ts = i.getIssuanceDate() == null ? null : i.getIssuanceDate().atStartOfDay();
                act.setTimestamp(ts);
                act.setTimeAgo(formatTimeAgo(ts));
                activities.add(act);
            }
        }

        if (returns != null) {
            for (ReturnRecord rr : returns) {
                String items = buildReturnItemList(rr);
                String loc = rr.getIssuance() != null ? rr.getIssuance().getLocation() : null;
                ActivityDto act = new ActivityDto("Tool Returned", rr.getIssuance() != null ? rr.getIssuance().getTrainerName() : "", "Mixed", items, rr.getActualReturnDate(), loc);
                LocalDateTime ts = rr.getActualReturnDate() == null ? null : rr.getActualReturnDate().atStartOfDay();
                act.setTimestamp(ts);
                act.setTimeAgo(formatTimeAgo(ts));
                activities.add(act);
            }
        }

        List<ActivityDto> sorted = activities.stream()
                .sorted((a,b) -> {
                    if (a.getDate() == null && b.getDate() == null) return 0;
                    if (a.getDate() == null) return 1;
                    if (b.getDate() == null) return -1;
                    return b.getDate().compareTo(a.getDate());
                })
                .limit(8)
                .collect(Collectors.toList());

        resp.setRecentActivities(sorted);

        // set totals in separate fields: repurpose totalTools/totalKits to show total issuance/returns
        resp.setTotalTools(totalIssuance);
        resp.setTotalKits(totalReturns);

        return resp;
    }

    private String buildItemList(List<Long> toolIds, List<Long> kitIds) {
        List<String> parts = new ArrayList<>();
        if (toolIds != null) {
            for (Long id : toolIds) {
                Tool t = toolRepository.findById(id).orElse(null);
                parts.add(t != null ? t.getDescription() : "Tool " + id);
            }
        }
        if (kitIds != null) {
            for (Long id : kitIds) {
                Kit k = kitRepository.findById(id).orElse(null);
                parts.add(k != null ? k.getKitName() : "Kit " + id);
            }
        }
        return String.join(", ", parts);
    }

    private String buildReturnItemList(ReturnRecord rr) {
        List<String> parts = new ArrayList<>();
        if (rr.getItems() != null) {
            for (ReturnItem ri : rr.getItems()) {
                if (ri.getToolId() != null) {
                    Tool t = toolRepository.findById(ri.getToolId()).orElse(null);
                    parts.add(t != null ? t.getDescription() : "Tool " + ri.getToolId());
                } else if (ri.getKitId() != null) {
                    Kit k = kitRepository.findById(ri.getKitId()).orElse(null);
                    parts.add(k != null ? k.getKitName() : "Kit " + ri.getKitId());
                }
            }
        }
        return String.join(", ", parts);
    }

    private String formatTimeAgo(LocalDateTime ts) {
        if (ts == null) return null;
        Duration d = Duration.between(ts, LocalDateTime.now());
        if (d.isNegative()) return "just now";
        long secs = d.getSeconds();
        if (secs < 60) return secs + " sec" + (secs != 1 ? "s" : "") + " ago";
        long mins = secs / 60;
        if (mins < 60) return mins + " min" + (mins != 1 ? "s" : "") + " ago";
        long hours = mins / 60;
        if (hours < 24) return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        long days = hours / 24;
        if (days < 30) return days + " day" + (days != 1 ? "s" : "") + " ago";
        long months = days / 30;
        if (months < 12) return months + " month" + (months != 1 ? "s" : "") + " ago";
        long years = months / 12;
        return years + " year" + (years != 1 ? "s" : "") + " ago";
    }
}
