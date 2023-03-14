package com.chuhang.framework.domain.course.request;

import com.chuhang.framework.model.request.RequestData;
import lombok.Data;
import lombok.ToString;

/**
 * Created by xf on 2018/4/13.
 */
@Data
@ToString
public class CourseListRequest extends RequestData {
    //公司Id
    private String companyId;
}
