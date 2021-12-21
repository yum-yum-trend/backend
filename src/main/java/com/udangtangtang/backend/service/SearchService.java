package com.udangtangtang.backend.service;

import com.udangtangtang.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TagRepository tagRepository;

    public List<String> getTagList() {
        return tagRepository.findDistinctNames();
    }
}
