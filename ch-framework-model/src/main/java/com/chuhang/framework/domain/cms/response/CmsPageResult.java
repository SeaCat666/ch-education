package com.chuhang.framework.domain.cms.response;
import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.model.response.ResultCode;
import com.chuhang.framework.model.response.ResponseResult;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CmsPageResult extends ResponseResult {
    CmsPage cmsPage;
    public CmsPageResult(ResultCode resultCode, CmsPage cmsPage) {
        super(resultCode);
        this.cmsPage = cmsPage;
    }
}
