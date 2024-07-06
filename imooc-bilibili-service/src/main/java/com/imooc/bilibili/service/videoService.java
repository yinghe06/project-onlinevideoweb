package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.VideoDao;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.util.FastdfsUtil;
import com.imooc.bilibili.service.util.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class videoService {

    @Autowired
   private VideoDao videoDao;
    @Autowired
    private FastdfsUtil fastdfsUtil;
    @Autowired
    private UserCoinService userCoinService;
    @Autowired
    private UserService userService;
    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(new Date());
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> videoTagList = video.getVideoTagList();
        videoTagList.forEach(item->{
                item.setCreateTime(now);
                item.setVideoId(videoId);
    });
        videoDao.batchAddVideoTag(videoTagList);
    }

    public PageResult<Video> pageListVideo(Integer size, Integer no, String area) {
        if(size==0||no==0){
            throw  new ConditionException("非法参数");
        }
        HashMap<Object, Object> params = new HashMap<>();
        params.put("start", (no-1)*size);
        params.put("limit",size);
        params.put("area", area);
        Integer total=videoDao.pageCountVideos(params);
        List<Video> list=new ArrayList<>();
        if(total>0){
           list= videoDao.pageListVideo(params);
        }
        return new PageResult<>(total,list);

    }

    public void viewVideoOnlineByslices(HttpServletRequest request, HttpServletResponse response, String url) {
        try {
            fastdfsUtil.viewVideoOnlineByslices(request,response,url);
        }catch (Exception ignore){

        }

    }

    public void addVideoLike(Long videoId, Long userId) {
       Video video= videoDao.getVideoById(videoId);
       if(video==null){
           throw new ConditionException("非法视频");
       }
      VideoLike videoLike= videoDao.getVideoLikeByVideoIdAndUserId(videoId,userId);
       if(videoLike!=null){
           throw new ConditionException("已点赞");

       }
       videoLike =new VideoLike();
       videoLike.setUserId(userId);
       videoLike.setVideoId(videoId);
       videoLike.setCreateTime(new Date());
       videoDao.addVideoLike(videoLike);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count=videoDao.getVideoLikes(videoId);
        VideoLike videolike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        boolean like=videolike!=null;
        Map<String, Object> result=new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;

    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId,userId);
    }

    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        if(videoId==null||groupId==null){
            throw new ConditionException("参数异常");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频");
        }
        videoDao.deleteVideoCollection(videoId,userId);
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
    videoDao.deleteVideoCollection(videoId,userId);
    }

    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoDao.getVideoCollections(videoId);
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }


    public void addVideoCoin(VideoCoin videoCoin, Long userId) {
        Long videoId = videoCoin.getVideoId();
        Integer amount = videoCoin.getAmount();
        if(videoId==null){
            throw new ConditionException("参数异常");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频");
        }
        //查询用户的硬币数量是否足够
       Integer userCoinsAmount= userCoinService.getUserCoinsAmount(userId);
        userCoinsAmount=userCoinsAmount==null?0:userCoinsAmount;
        if (userCoinsAmount<videoCoin.getAmount()){
            throw new ConditionException("硬币数量不足");
        }
        //查询是否已经投币过
        VideoCoin dbVideoCoin=videoDao.getVideoCoinsByVideoIdAndUserId(videoId,userId);
        if (dbVideoCoin==null){
            videoCoin.setUserId(userId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        }else{
           Integer dbcoins= dbVideoCoin.getAmount();
            dbcoins+=amount;
            videoCoin.setAmount(dbcoins);
            videoCoin.setUserId(userId);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        //更新用户硬币账户
        userCoinService.updateUserCoinsAmount(userId,(userCoinsAmount-amount));
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long count= videoDao.getVideoCoins(videoId);
        VideoCoin videoCoins= videoDao.getVideoCoinsByVideoIdAndUserId(videoId, userId);
        boolean like=videoCoins!=null?true:false;
        Map<String, Object> map=new HashMap<>();
        map.put("count", count);
        map.put("like", like);
        return new JSONObject(map);
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        Long videoId = videoComment.getVideoId();

        if(videoId==null){
            throw new ConditionException("参数异常");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频");
        }
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);



    }

    public PageResult<VideoComment> getVideoComments(Integer size, Integer no, Long videoId) {
        if(videoId==null){
            throw new ConditionException("参数异常");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频");
        }
        Map<String,Object> map=new HashMap<>();
        int start=size*(no-1);
        int limit=size;
        map.put("start", start);
        map.put("limit", limit);
        map.put("videoId", videoId);
       int  total=videoDao.pageCountVideoComment(map);
       List<VideoComment> list=new ArrayList<>();
       if(total>0){
          list= videoDao.getVideoComments(map);
           List<Long> parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
           List<VideoComment> childCommentList=videoDao.batchGetCommentByRootId(parentIdList);
           //查询用户信息
           Set<Long> userIdList = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
           Set<Long> childUserIdList = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
           Set<Long> replyUserIdList = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
           userIdList.addAll(childUserIdList);
           userIdList.addAll(replyUserIdList);
           List<UserInfo> userInfoList= userService.batchGetUserInfoById(userIdList);
           Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
           list.forEach(comment->{
               Long id=comment.getId();
               List<VideoComment> childList=new ArrayList<>();
               childCommentList.forEach(child->{
                   if (id.equals(child.getRootId())){
                       child.setUserInfo(userInfoMap.get(child.getUserId()));
                       child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                       childList.add(child);
                   }
               });
               comment.setChildList(childList);
               comment.setUserInfo(userInfoMap.get(comment.getUserId()));
           });

       }
       return new PageResult<>(total, list);
    }

    public void addVideoView(VideoView videoView, HttpServletRequest httpServletRequest) {
        Long videoId = videoView.getVideoId();
        Long userId = videoView.getUserId();
        String agent = httpServletRequest.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(httpServletRequest);
        HashMap<String, Object> param = new HashMap<>();
        if (userId!=null){
            param.put("userId",userId);
        }else{
            param.put("clientId", clientId);
            param.put("ip", ip);
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        param.put("today", sdf.format(now));
        param.put("videoId", videoId);
       VideoView dbVideoView= videoDao.getVideoView(param);
       if (dbVideoView==null){
           videoView.setClientId(clientId);
           videoView.setIp(ip);
           videoView.setCreateTime(new Date());
           System.out.println(videoView.getVideoId());
           videoDao.addVideoView(videoView);
       }


    }

    public Integer getVideoViewCount(Long videoId) {
       return videoDao.getVideoViewCount(videoId);
    }

    public List<Video> recommend(Long userId) throws TasteException {
        List<UserPreference> list=videoDao.getAllUserPreference();
        DataModel dataModel= this.createDataModel(list);
        UncenteredCosineSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        System.out.println(similarity.userSimilarity(5, 7));
        NearestNUserNeighborhood nearestNUserNeighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        long[] arr = nearestNUserNeighborhood.getUserNeighborhood(userId);
        Recommender recommender = new GenericUserBasedRecommender(dataModel, nearestNUserNeighborhood, similarity);
        List<RecommendedItem> recommendedItems = recommender.recommend(userId, 2);
        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        return videoDao.batchGetVideoByIds(itemIds);
    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        //
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
        Collection<List<UserPreference>> list = map.values();
        for(List<UserPreference> userPreferences : list){
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for(int i = 0; i < userPreferences.size(); i++){
                UserPreference userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }

}
