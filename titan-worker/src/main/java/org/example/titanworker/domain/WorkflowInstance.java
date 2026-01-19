package org.example.titanworker.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_instances")
@Getter
@Setter
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL)
    private List<TaskInstance> tasks = new ArrayList<>();
}
