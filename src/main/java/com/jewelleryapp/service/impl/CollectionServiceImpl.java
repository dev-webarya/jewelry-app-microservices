package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.CollectionRequestDto;
import com.jewelleryapp.dto.response.CollectionResponseDto;
import com.jewelleryapp.entity.Collection;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.CollectionMapper;
import com.jewelleryapp.repository.CollectionRepository;
import com.jewelleryapp.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;

    @Override
    @Transactional
    public CollectionResponseDto createCollection(CollectionRequestDto requestDto) {
        if (collectionNameExists(requestDto.getName())) {
            throw new DuplicateResourceException("Collection with name '" + requestDto.getName() + "' already exists.");
        }

        Collection collection = collectionMapper.toEntity(requestDto);
        Collection savedCollection = collectionRepository.save(collection);
        return collectionMapper.toDto(savedCollection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollectionResponseDto> getAllCollections(Specification<Collection> spec, Pageable pageable) {
        return collectionRepository.findAll(spec, pageable)
                .map(collectionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionResponseDto getCollectionById(Integer id) {
        return collectionRepository.findById(id)
                .map(collectionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id));
    }

    @Override
    @Transactional
    public CollectionResponseDto updateCollection(Integer id, CollectionRequestDto requestDto) {
        Collection existingCollection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id));

        if (!existingCollection.getName().equalsIgnoreCase(requestDto.getName()) && collectionNameExists(requestDto.getName())) {
            throw new DuplicateResourceException("Collection with name '" + requestDto.getName() + "' already exists.");
        }

        collectionMapper.updateEntityFromDto(requestDto, existingCollection);

        Collection updatedCollection = collectionRepository.save(existingCollection);
        return collectionMapper.toDto(updatedCollection);
    }

    @Override
    @Transactional
    public void deleteCollection(Integer id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id));

        if (!collection.getProducts().isEmpty()) {
            throw new DataIntegrityViolationException("Cannot delete collection. There are products associated with it.");
        }

        collectionRepository.delete(collection);
    }

    private boolean collectionNameExists(String name) {
        return collectionRepository.exists((root, query, cb) ->
                cb.equal(cb.lower(root.get("name")), name.toLowerCase())
        );
    }
}