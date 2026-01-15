package org.example.titanorchestrator.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "task_definitions")
@Getter
@Setter
public class TaskDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private Tasktype taskType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "retry_count")
    private int retryCount = 3;

    @Column(name = "timeout_seconds")
    private int timeoutSeconds = 3600;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "parent_task_id"),
            inverseJoinColumns = @JoinColumn(name = "child_task_id")
    )

    private Set<TaskDefinition> nextTasks = new HashSet<>();

    @ManyToMany(mappedBy = "nextTasks", fetch = FetchType.LAZY)
    private Set<TaskDefinition> previousTasks = new HashSet<>();

    public void addNextTask(TaskDefinition task) {
        this.nextTasks.add(task);
        task.getPreviousTasks().add(this);
    }


    public void removeNextTask(TaskDefinition task) {
        this.nextTasks.remove(task);
        task.getPreviousTasks().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskDefinition)) return false;
        return id != null && id.equals(((TaskDefinition) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
