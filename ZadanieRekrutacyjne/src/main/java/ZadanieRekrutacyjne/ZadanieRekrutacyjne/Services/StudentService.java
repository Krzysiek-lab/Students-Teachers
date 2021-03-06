package ZadanieRekrutacyjne.ZadanieRekrutacyjne.Services;

import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Entities.Student;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Entities.Teacher;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Repositories.StudentRepository;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.Repositories.TeacherRepository;
import ZadanieRekrutacyjne.ZadanieRekrutacyjne.ViewModels.StudentViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public Student Add(StudentViewModel studentViewModel) {
        var teachers = teacherRepository.findAllById(studentViewModel.getTeachers_ids());

        var newStudent = Student.builder()
                .name(studentViewModel.getName())
                .lastName(studentViewModel.getLastName())
                .age(studentViewModel.getAge())
                .email(studentViewModel.getEmail())
                .field(studentViewModel.getField())
                .teachers(teachers)
                .build();

        return studentRepository.save(newStudent);
    }

    public Student Get(Long studentId) {
        return studentRepository.getOne(studentId);
    }

    public List<Student> GetAll() {
        return studentRepository.findAll();
    }

    public Page<Student> GetPage(int page, int pageSize, String columnName, boolean asc) {
        var sortable = asc ? Sort.by(columnName).ascending() : Sort.by(columnName).descending();
        return studentRepository.findAll(PageRequest.of(page, pageSize, sortable));
    }

    public List<Student> GetByName(String name) {
        var byName = studentRepository.GetByName(name);
        var byLastName = studentRepository.GetByLastName(name);

        var allWithDuplicates = new ArrayList<>(byName);
        allWithDuplicates.addAll(byLastName);

        return new ArrayList<>(new HashSet<>(allWithDuplicates));
    }

    public List<Teacher> GetForStudent(Long studentId) {
        return studentRepository.getOne(studentId).getTeachers();
    }

    public Student Update(StudentViewModel studentViewModel) {
        var student = studentRepository.getOne(studentViewModel.getId());

        student.setName(studentViewModel.getName());
        student.setLastName(studentViewModel.getLastName());
        student.setAge(studentViewModel.getAge());
        student.setEmail(studentViewModel.getEmail());
        student.setField(studentViewModel.getField());

        return studentRepository.save(student);
    }

    public void Delete(Long studentId) {
        var student = studentRepository.getOne(studentId);

        studentRepository.delete(student);
    }
}
