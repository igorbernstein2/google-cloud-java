// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/cloud/videointelligence/v1beta1/video_intelligence.proto

package com.google.cloud.videointelligence.v1beta1;

public interface VideoContextOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.cloud.videointelligence.v1beta1.VideoContext)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Video segments to annotate. The segments may overlap and are not required
   * to be contiguous or span the whole video. If unspecified, each video
   * is treated as a single segment.
   * </pre>
   *
   * <code>repeated .google.cloud.videointelligence.v1beta1.VideoSegment segments = 1;</code>
   */
  java.util.List<com.google.cloud.videointelligence.v1beta1.VideoSegment> 
      getSegmentsList();
  /**
   * <pre>
   * Video segments to annotate. The segments may overlap and are not required
   * to be contiguous or span the whole video. If unspecified, each video
   * is treated as a single segment.
   * </pre>
   *
   * <code>repeated .google.cloud.videointelligence.v1beta1.VideoSegment segments = 1;</code>
   */
  com.google.cloud.videointelligence.v1beta1.VideoSegment getSegments(int index);
  /**
   * <pre>
   * Video segments to annotate. The segments may overlap and are not required
   * to be contiguous or span the whole video. If unspecified, each video
   * is treated as a single segment.
   * </pre>
   *
   * <code>repeated .google.cloud.videointelligence.v1beta1.VideoSegment segments = 1;</code>
   */
  int getSegmentsCount();
  /**
   * <pre>
   * Video segments to annotate. The segments may overlap and are not required
   * to be contiguous or span the whole video. If unspecified, each video
   * is treated as a single segment.
   * </pre>
   *
   * <code>repeated .google.cloud.videointelligence.v1beta1.VideoSegment segments = 1;</code>
   */
  java.util.List<? extends com.google.cloud.videointelligence.v1beta1.VideoSegmentOrBuilder> 
      getSegmentsOrBuilderList();
  /**
   * <pre>
   * Video segments to annotate. The segments may overlap and are not required
   * to be contiguous or span the whole video. If unspecified, each video
   * is treated as a single segment.
   * </pre>
   *
   * <code>repeated .google.cloud.videointelligence.v1beta1.VideoSegment segments = 1;</code>
   */
  com.google.cloud.videointelligence.v1beta1.VideoSegmentOrBuilder getSegmentsOrBuilder(
      int index);

  /**
   * <pre>
   * If label detection has been requested, what labels should be detected
   * in addition to video-level labels or segment-level labels. If unspecified,
   * defaults to `SHOT_MODE`.
   * </pre>
   *
   * <code>.google.cloud.videointelligence.v1beta1.LabelDetectionMode label_detection_mode = 2;</code>
   */
  int getLabelDetectionModeValue();
  /**
   * <pre>
   * If label detection has been requested, what labels should be detected
   * in addition to video-level labels or segment-level labels. If unspecified,
   * defaults to `SHOT_MODE`.
   * </pre>
   *
   * <code>.google.cloud.videointelligence.v1beta1.LabelDetectionMode label_detection_mode = 2;</code>
   */
  com.google.cloud.videointelligence.v1beta1.LabelDetectionMode getLabelDetectionMode();

  /**
   * <pre>
   * Whether the video has been shot from a stationary (i.e. non-moving) camera.
   * When set to true, might improve detection accuracy for moving objects.
   * </pre>
   *
   * <code>bool stationary_camera = 3;</code>
   */
  boolean getStationaryCamera();

  /**
   * <pre>
   * Model to use for label detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string label_detection_model = 4;</code>
   */
  java.lang.String getLabelDetectionModel();
  /**
   * <pre>
   * Model to use for label detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string label_detection_model = 4;</code>
   */
  com.google.protobuf.ByteString
      getLabelDetectionModelBytes();

  /**
   * <pre>
   * Model to use for face detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string face_detection_model = 5;</code>
   */
  java.lang.String getFaceDetectionModel();
  /**
   * <pre>
   * Model to use for face detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string face_detection_model = 5;</code>
   */
  com.google.protobuf.ByteString
      getFaceDetectionModelBytes();

  /**
   * <pre>
   * Model to use for shot change detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string shot_change_detection_model = 6;</code>
   */
  java.lang.String getShotChangeDetectionModel();
  /**
   * <pre>
   * Model to use for shot change detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string shot_change_detection_model = 6;</code>
   */
  com.google.protobuf.ByteString
      getShotChangeDetectionModelBytes();

  /**
   * <pre>
   * Model to use for safe search detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string safe_search_detection_model = 7;</code>
   */
  java.lang.String getSafeSearchDetectionModel();
  /**
   * <pre>
   * Model to use for safe search detection.
   * Supported values: "latest" and "stable" (the default).
   * </pre>
   *
   * <code>string safe_search_detection_model = 7;</code>
   */
  com.google.protobuf.ByteString
      getSafeSearchDetectionModelBytes();
}