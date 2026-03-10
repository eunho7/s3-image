package com.example.image.mapper;

import com.example.image.entity.ImageAttachment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ImageAttachmentMapper {

    int insertImage(ImageAttachment imageAttachment);

    ImageAttachment findByImageSeq(@Param("imageSeq") Long imageSeq);

    List<ImageAttachment> findAllActiveImages();

    int updateMetadata(ImageAttachment imageAttachment);

    int softDeleteImage(@Param("imageSeq") Long imageSeq, @Param("updateUser") String updateUser);
}
