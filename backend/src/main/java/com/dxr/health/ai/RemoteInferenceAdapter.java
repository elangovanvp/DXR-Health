package com.dxr.health.ai;

import com.dxr.health.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component("remoteInferenceAdapter")
public class RemoteInferenceAdapter implements TriageModelAdapter {

    private static final Logger log = LoggerFactory.getLogger(RemoteInferenceAdapter.class);

    private final String remoteUrl;
    private final RestTemplate restTemplate;

    public RemoteInferenceAdapter(@Value("${ai.adapter.remote-inference-url:http://localhost:8000/v1/infer}") String remoteUrl) {
        this.remoteUrl = remoteUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getAdapterName() {
        return "dxr-remote-onnx";
    }

    @Override
    public String getModelVersion() {
        return "2.1.0";
    }

    @Override
    @SuppressWarnings("unchecked")
    public TriageResult performTriage(ImagingStudy study, ImageAsset imageAsset) {
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> request = Map.of(
                    "studyId", study.getStudyId(),
                    "objectKey", imageAsset.getObjectKey(),
                    "modality", study.getModality(),
                    "bodyRegion", study.getBodyRegion()
            );

            Map<String, Object> response = restTemplate.postForObject(remoteUrl, request, Map.class);
            long duration = System.currentTimeMillis() - startTime;

            String priorityStr = response != null && response.containsKey("priority")
                    ? response.get("priority").toString().toUpperCase()
                    : "ROUTINE";
            TriagePriority priority = TriagePriority.valueOf(priorityStr);
            Double confidence = response != null && response.containsKey("confidence")
                    ? Double.valueOf(response.get("confidence").toString())
                    : 0.85;

            List<String> labels = response != null && response.containsKey("candidateLabels")
                    ? (List<String>) response.get("candidateLabels")
                    : List.of("Remote Model Evaluation");

            TriageResult result = new TriageResult(study.getStudyId(), priority, confidence, getAdapterName(), getModelVersion());
            result.setCandidateLabelsList(labels);
            result.setStatus(TriageStatus.READY);
            result.setProcessingTimeMs(duration);
            return result;
        } catch (Exception e) {
            log.warn("Remote inference service call failed: {}. Throwing exception to trigger safe fallback.", e.getMessage());
            throw new RuntimeException("Remote inference error: " + e.getMessage(), e);
        }
    }
}
