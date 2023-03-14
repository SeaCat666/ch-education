package com.chuhang.framework.domain.ucenter.ext;

import com.chuhang.framework.domain.ucenter.ChMenu;
import com.chuhang.framework.domain.course.ext.CategoryNode;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Created by admin on 2018/3/20.
 */
@Data
@ToString
public class ChMenuExt extends ChMenu {

    List<CategoryNode> children;
}
