package com.imooc.bilibili;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.service.ElasticSearchService;
import com.imooc.bilibili.service.videoService;
import com.imooc.bilibili.support.UserSupport;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
public class VideoApi {
    @Autowired
    private UserSupport userSupport;
    @Autowired
    private videoService videoService;
    @Autowired
    private ElasticSearchService elasticSearchService;
    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video){
        Long userId = userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.addVideos(video);
        elasticSearchService.addVideo(video);
        return JsonResponse.success();
    }
    @GetMapping("/video")
    public JsonResponse<PageResult<Video>> pageListVideo(Integer size, Integer no, String area){
        PageResult<Video> result=videoService.pageListVideo(size,no,area);
        return new JsonResponse<>(result);
    }
    @GetMapping("/video-slices")
    public void viewVideoOnlineByslices(HttpServletRequest request, HttpServletResponse response,String url){
        videoService.viewVideoOnlineByslices(request,response,url);
    }
    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId,userId);
        return JsonResponse.success();
    }
    @GetMapping("/video-likes")
    public JsonResponse<Map<String,Object>> getVideoLikes(@RequestParam Long videoId){
        Long userId=null;
        try{
            userId = userSupport.getCurrentUserId();
        }catch (Exception e){}
        Map<String,Object> result =videoService.getVideoLikes(videoId,userId);
        return new JsonResponse<>(result);
    }
    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideoLike(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId,userId);
        return JsonResponse.success();
    }
    @PostMapping("/video-collections")
    public JsonResponse<String> addVideoCollection(@RequestBody VideoCollection videoCollection){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCollection(videoCollection, userId);
        return JsonResponse.success();
    }

    /**
     * 取消收藏视频
     */
    @DeleteMapping("/video-collections")
    public JsonResponse<String> deleteVideoCollection(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 查询视频收藏数量
     */
    @GetMapping("/video-collections")
    public JsonResponse<Map<String, Object>> getVideoCollections(@RequestParam Long videoId){
        Long userId = null;
        try{
            userId = userSupport.getCurrentUserId();
        }catch (Exception ignored){}
        Map<String, Object> result = videoService.getVideoCollections(videoId, userId);
        return new JsonResponse<>(result);
    }
    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCoin(videoCoin, userId);
        return JsonResponse.success();
    }
    @GetMapping("/video-coins")
    public JsonResponse<Map<String,Object>> getVideoCoins(@RequestParam Long videoId){
        Long userId=null;
        try {
            userId = userSupport.getCurrentUserId();
        }catch (Exception e){
        }
        Map<String, Object> result =videoService.getVideoCoins(videoId,userId);
        return new JsonResponse<>(result);
    }
    @PostMapping("/video-comment")
    public JsonResponse<String> addVideoComment(@RequestBody VideoComment videoComment){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment, userId);
        return JsonResponse.success();
    }

    @GetMapping("/video-comment")
    public JsonResponse<PageResult<VideoComment>> getVideoComments(@RequestParam Integer size,
                                                                @RequestParam Integer no,
                                                                @RequestParam Long videoId){
        PageResult<VideoComment> result= videoService.getVideoComments(size,no,videoId);
        return new JsonResponse<>(result);
    }
    @PostMapping("/video-view")
    public JsonResponse<String> addVideoView(@RequestBody VideoView videoView,
                                             HttpServletRequest httpServletRequest){
        Long userId;
        try {
             userId = userSupport.getCurrentUserId();
             videoView.setUserId(userId);
             videoService.addVideoView(videoView,httpServletRequest);
        }catch (Exception e){
            videoService.addVideoView(videoView,httpServletRequest);
        }
        return JsonResponse.success();

    }
    @GetMapping("/video-view-counts")
    public JsonResponse<Integer> getVideoViewCount(@RequestParam Long videoId){
       Integer count= videoService.getVideoViewCount(videoId);
       return new JsonResponse<>(count);
    }
    @GetMapping("/recommendations")
    public JsonResponse<List<Video>> recommend() {
        Long userId = userSupport.getCurrentUserId();
        List<Video> videoList= videoService.recommend(userId);
        return new JsonResponse<>(videoList);
    }
}
