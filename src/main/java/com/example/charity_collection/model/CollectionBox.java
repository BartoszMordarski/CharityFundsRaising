package com.example.charity_collection.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collection_box")
@Data
public class CollectionBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String identifier;

    @Column(name = "is_empty", nullable = false)
    private Boolean isEmpty = true;

    @ManyToOne
    @JoinColumn(name = "fundrising_event_id")
    private FundraisingEvent fundraisingEvent;

    @OneToMany(mappedBy = "collectionBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionBoxMoney> money = new ArrayList<>();
}
