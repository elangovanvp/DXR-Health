package com.dxr.health.ai;

import com.dxr.health.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("mockTriageModelAdapter")
public class MockTriageModelAdapter implements TriageModelAdapter {

    private static final String MODEL_NAME = "dxr-triage-mock";
    private static final String MODEL_VERSION = "1.0.0";

    @Override
    public String getAdapterName() {
        return MODEL_NAME;
    }

    @Override
    public String getModelVersion() {
        return MODEL_VERSION;
    }

    @Override
    public TriageResult performTriage(ImagingStudy study, ImageAsset imageAsset) {
        long startTime = System.currentTimeMillis();

        String indication = study.getClinicalIndication() != null ? study.getClinicalIndication().toLowerCase() : "";

        TriagePriority priority;
        double confidence;
        List<String> labels;

        if (indication.contains("pneumothorax") || indication.contains("hemorrhage") ||
                indication.contains("critical") || indication.contains("tension") || indication.contains("acute")) {
            priority = TriagePriority.CRITICAL;
            confidence = 0.94;
            labels = List.of("Tension Pneumothorax", "Pleural Effusion Suspicion");
        } else if (indication.contains("pneumonia") || indication.contains("consolidation") ||
                indication.contains("urgent") || indication.contains("infiltrate") || indication.contains("shortness of breath")) {
            priority = TriagePriority.HIGH;
            confidence = 0.86;
            labels = List.of("Bilateral Consolidation", "Suspected Lower Lobe Pneumonia");
        } else {
            priority = TriagePriority.ROUTINE;
            confidence = 0.73;
            labels = List.of("No Acute Cardiopulmonary Abnormality");
        }

        long duration = System.currentTimeMillis() - startTime;
        if (duration == 0) duration = 15; // telemetry baseline

        TriageResult result = new TriageResult(study.getStudyId(), priority, confidence, MODEL_NAME, MODEL_VERSION);
        result.setCandidateLabelsList(labels);
        result.setStatus(TriageStatus.READY);
        result.setProcessingTimeMs(duration);
        result.setExplanationRef("artifacts/explanations/" + study.getStudyId() + "-heatmap.json");
        return result;
    }
}
