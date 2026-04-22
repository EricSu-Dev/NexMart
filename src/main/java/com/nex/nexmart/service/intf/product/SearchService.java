package com.nex.nexmart.service.intf.product;

import java.util.List;

public interface SearchService {
	void recordKeyword(String keyword);
	List<String> getHotKeywords();
}
