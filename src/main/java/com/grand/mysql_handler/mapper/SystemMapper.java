package com.grand.mysql_handler.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemMapper {

	@Insert("${sql}")
	int insertBySql(@Param("sql")String sql);
	@Delete("${sql}")
	int deleteBySql(@Param("sql")String sql);
	@Update("${sql}")
	int updateBySql(@Param("sql")String sql);
	@Select("${sql}")
	List<Map<String,Object>> selectBySql(@Param("sql")String sql);
	@Select("${sql}")
	Map<String,Object> selectOneBySql(@Param("sql")String sql);

}
