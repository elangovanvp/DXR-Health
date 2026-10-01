package com.dxr.health.ai;

import com.dxr.health.model.ImageAsset;
import com.dxr.health.model.ImagingStudy;
import com.dxr.health.model.TriageResult;

public interface TriageModelAdapter {
    String getAdapterName();
    String getModelVersion();
    TriageResult performTriage(ImagingStudy study, ImageAsset imageAsset);
}
