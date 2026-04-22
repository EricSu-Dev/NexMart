package com.nex.nexmart.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

	/** 当前页数据 */
	private List<T> records;
	/** 总记录数 */
	private long total;
	/** 当前页码 */
	private long current;
	/** 每页条数 */
	private long size;
	/** 总页数 */
	private long pages;

	/** 额外信息：平均评分（仅评价列表使用） */
	private Double avgRating;

	private PageResult() {}

	/**
	 * 从 MyBatis-Plus IPage 直接转换
	 */
	public static <T> PageResult<T> of(IPage<T> page) {
		PageResult<T> result = new PageResult<>();
		result.setRecords(page.getRecords());
		result.setTotal(page.getTotal());
		result.setCurrent(page.getCurrent());
		result.setSize(page.getSize());
		result.setPages(page.getPages());
		return result;
	}

	public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
		PageResult<T> result = new PageResult<>();
		result.setRecords(records);
		result.setTotal(total);
		result.setCurrent(current);
		result.setSize(size);
		result.setPages((total + size - 1) / size); // 计算总页数
		return result;
	}

}


