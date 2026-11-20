# HAJIRI BOOK – AI & TFLITE FACE RECOGNITION GUIDE

## 1. Overview of AI Architecture
Face recognition in Hajiri Book operates completely **on-device** using TensorFlow Lite to ensure instant recognition, maximum student privacy, and full offline capability without internet connectivity.

The pipeline comprises 4 distinct stages:
```
Camera Frame (CameraX) 
   │
   ▼
1. Face Detection & Tracking (TFLite Face Detector + MultiFaceTracker)
   │
   ▼
2. Anti-Spoofing & Liveness Check (LivenessDetector)
   │
   ▼
3. Feature Embedding Extraction (MobileFaceNet TFLite)
   │
   ▼
4. Vector Matching Engine (Cosine Similarity & Dynamic Threshold)
   │
   ▼
Attendance Stored in Room & Announced via TextToSpeech
```

---

## 2. Model Specifications

### Model 1: Face Detector (`face_detector.tflite`)
- **Architecture**: UltraFace / BlazeFace / SSD MobileNetV2
- **Input Dimensions**: `1 x 240 x 320 x 3` (Float32)
- **Pixel Normalization**: `(pixel - 127.5) / 128.0` in range `[-1.0, 1.0]`
- **Outputs**:
  - `boxes`: `[1, 4420, 4]` (normalized bounding boxes `[ymin, xmin, ymax, xmax]`)
  - `scores`: `[1, 4420, 2]` (confidence scores for face vs background)
- **Recommended Threshold**: `0.65`

### Model 2: Face Embedder (`mobilefacenet.tflite`)
- **Architecture**: MobileFaceNet (Lightweight deep convolutional neural network optimized for mobile biometric recognition)
- **Input Dimensions**: `1 x 112 x 112 x 3` (Float32, RGB)
- **Output Dimensions**: `1 x 192` (Float32 vector)
- **Normalization**:
  1. Input image normalized using standard:
     $$\hat{x} = \frac{x - 127.5}{128.0}$$
  2. Output embedding vector L2-normalized:
     $$v_{norm} = \frac{v}{\sqrt{\sum_{i=1}^{192} v_i^2}}$$

---

## 3. Cosine Similarity Matching Engine
To recognize a student, the probe embedding $\vec{P}$ is compared against all enrolled templates $\vec{E}_k$ using cosine similarity:

$$\text{Similarity}(\vec{P}, \vec{E}) = \frac{\vec{P} \cdot \vec{E}}{\|\vec{P}\|_2 \|\vec{E}\|_2}$$

Since both vectors are already L2-normalized ($\|\vec{P}\|_2 = \|\vec{E}\|_2 = 1.0$):
$$\text{Similarity}(\vec{P}, \vec{E}) = \sum_{i=1}^{192} P_i \cdot E_i$$

### Threshold Configuration:
- **Default Threshold**: `0.72`
- **Conservative (High Security)**: `0.78`
- **Tolerant (Variable Classroom Lighting)**: `0.68`
- If maximum similarity is strictly less than the configured threshold, the match is classified as **"Unknown Face"** and no attendance is marked.

---

## 4. Multi-Face Tracking & Cooldown
- Multiple students appearing simultaneously in the camera viewport are assigned unique tracking IDs via Intersection-over-Union (IoU) matching across consecutive frames.
- A **cooldown period of 8 seconds** is enforced per tracked face ID to prevent duplicate attendance writes and repeated voice announcements.

---

## 5. How to Obtain Model Weights
1. **MobileFaceNet**:
   - Official InsightFace Repository: [InsightFace GitHub](https://github.com/deepinsight/insightface)
   - Pre-trained MobileFaceNet TFLite models are available under open-source licenses on GitHub (e.g. `sirius-ai/MobileFaceNet_TF`).
2. Place the converted `.tflite` files inside:
   ```
   android/app/src/main/assets/models/mobilefacenet.tflite
   android/app/src/main/assets/models/face_detector.tflite
   ```
3. The application includes a deterministic feature-extraction fallback engine so the entire app builds and functions even before custom weights are added.
