package com.example.demo.services;

import com.example.demo.dto.request.ProjectRequest;
import com.example.demo.entity.*;
import com.example.demo.enums.Role;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public Project create(ProjectRequest request, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        User pm = null;
        if (request.getPmId() != null) {
            pm = userRepository.findById(request.getPmId())
                    .orElseThrow(() -> new ResourceNotFoundException("ПМ не найден"));

            if (pm.getRole() != Role.PM) {
                throw new IllegalArgumentException("Назначенный пользователь должен иметь роль PM");
            }
        }

        Department department = null;

        // ADMIN — может сам указать departmentId
        if (currentUser.getRole() == Role.ADMIN) {
            if (request.getDepartmentId() != null) {
                department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Отдел не найден"));
            }
        }

        // MANAGER — только свой департамент
        else if (currentUser.getRole() == Role.MANAGER) {
            department = departmentRepository.findByManagerId(currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("У руководителя нет назначенного департамента"));
        }

        else {
            throw new AccessDeniedException("У вас нет прав на создание проекта");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pm(pm)
                .department(department)
                .build();

        return projectRepository.save(project);
    }

    public List<Project> getAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Department department = null;
        if(user.getRole() == Role.MANAGER){
            department = departmentRepository.findByManagerId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager не состоит в департаментах"));
        }

        return switch (user.getRole()) {
            case ADMIN -> projectRepository.findAll();
            case MANAGER -> projectRepository.findByDepartmentId(department.getId());
            case PM -> projectRepository.findByPmId(user.getId());
            case TEAM -> projectRepository.findByMemberUserId(user.getId()); // через project_members
        };
    }

    public Project getById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект не найден: " + id));

        // ADMIN — видит всё
        if (user.getRole() == Role.ADMIN) {
            return p;
        }

        // MANAGER — только проекты своего департамента
        if (user.getRole() == Role.MANAGER) {
            if (!Objects.equals(p.getDepartment().getManager().getId(), user.getId())) {
                throw new AccessDeniedException("У руководителя нет назначенного департамента");
            }
            return p;
        }

        // PM — только свои проекты
        if (user.getRole() == Role.PM) {
            if (p.getPm() == null || !p.getPm().getId().equals(user.getId())) {
                throw new AccessDeniedException("Нет доступа к чужому проекту");
            }

            return p;
        }

        // TEAM — только если состоит в проекте
        if (user.getRole() == Role.TEAM) {
            boolean isMember = p.getMembers() != null &&
                    p.getMembers().stream()
                            .anyMatch(m -> m.getUser().getId().equals(user.getId()));

            if (!isMember) {
                throw new AccessDeniedException("Нет доступа к проекту");
            }

            return p;
        }

        throw new AccessDeniedException("У вас нет прав на просмотр проекта");
    }

    public Project update(Long id, ProjectRequest request, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Project project = getById(id, email);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            project.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        // ADMIN может менять департамент вручную
        if (currentUser.getRole() == Role.ADMIN) {
            if (request.getDepartmentId() != null) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Отдел не найден"));
                project.setDepartment(department);
            }
        }

        // MANAGER не может менять проект на чужой департамент
        if (currentUser.getRole() == Role.MANAGER) {
            if (project.getDepartment() == null ||
                    project.getDepartment().getManager() == null ||
                    !project.getDepartment().getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Вы можете редактировать только проекты своего департамента");
            }
        }

        // смена PM
        if (request.getPmId() != null) {
            User pm = userRepository.findById(request.getPmId())
                    .orElseThrow(() -> new ResourceNotFoundException("ПМ не найден"));

            if (pm.getRole() != Role.PM) {
                throw new IllegalArgumentException("Назначенный пользователь должен иметь роль PM");
            }

            // если у проекта есть департамент — PM должен быть из того же департамента
            if (project.getDepartment() != null) {
                if (pm.getDepartment() == null ||
                        !pm.getDepartment().getId().equals(project.getDepartment().getId())) {
                    throw new IllegalArgumentException("PM должен принадлежать тому же департаменту, что и проект");
                }
            }

            project.setPm(pm);
        }

        return projectRepository.save(project);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
