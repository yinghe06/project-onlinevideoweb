package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.domain.auth.AuthRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface VideoDao {


    void addVideos(Video video);

    void batchAddVideoTag(List<VideoTag> videoTagList);

    Integer pageCountVideos(HashMap<Object, Object> params);

    List<Video> pageListVideo(HashMap<Object, Object> params);

    Video getVideoById(Long videoId);

    VideoLike getVideoLikeByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    void addVideoLike(VideoLike videoLike);

    Long getVideoLikes(Long videoId);

    void deleteVideoLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    void deleteVideoCollection(@Param("videoId") Long videoId, @Param("userId") Long userId);

    void addVideoCollection(VideoCollection videoCollection);

    Long getVideoCollections(Long videoId);

    VideoCollection getVideoCollectionByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    VideoCoin getVideoCoinsByVideoIdAndUserId(Long videoId, Long userId);

    void addVideoCoin(VideoCoin videoCoin);

    Long getVideoCoins(Long videoId);

    Integer updateVideoCoin(VideoCoin videoCoin);

    void addVideoComment(VideoComment videoComment);

    int pageCountVideoComment(Map<String,Object> map);

    List<VideoComment> getVideoComments(Map<String,Object> map);

    List<VideoComment> batchGetCommentByRootId(@Param("rootIdList") List<Long> rootIdList);

    VideoView getVideoView(HashMap<String, Object> param);

    Integer getVideoViewCount(Long videoId);

    void addVideoView(VideoView videoView);

    List<UserPreference> getAllUserPreference();

    List<Video> batchGetVideoByIds(List<Long> itemIds);
}
