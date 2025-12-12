package ai.rupheus.application.dto.admin;

import ai.rupheus.application.dto.shared.PageableResponse;
import ai.rupheus.application.model.user.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public static User fromEntity(UserModel userModel) {
        User user = new User();
        user.setId(userModel.getId());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setEmail(userModel.getEmail());
        user.setPassword(userModel.getPassword());
        return user;
    }

    public static PageableResponse<List<User>> fromPage(Page<UserModel> userModelPage) {
        List<User> users = userModelPage.getContent()
            .stream()
            .map(User::fromEntity)
            .toList();

        PageableResponse<List<User>> pageableResponse = new PageableResponse<>();
        pageableResponse.setContent(users);

        PageableResponse.PageableInfo pageableInfo = new PageableResponse.PageableInfo();
        pageableInfo.setPage(userModelPage.getNumber());
        pageableInfo.setSize(userModelPage.getSize());
        pageableInfo.setTotalItems(userModelPage.getTotalElements());
        pageableInfo.setTotalPages(userModelPage.getTotalPages());

        pageableInfo.setHasNext(userModelPage.hasNext());
        pageableInfo.setHasPrevious(userModelPage.hasPrevious());
        pageableInfo.setFirst(userModelPage.isFirst());
        pageableInfo.setLast(userModelPage.isLast());

        PageableResponse.SortInfo sortInfo = new PageableResponse.SortInfo();
        sortInfo.setSorted(userModelPage.getSort().isSorted());
        sortInfo.setUnsorted(userModelPage.getSort().isUnsorted());
        sortInfo.setEmpty(userModelPage.getSort().isEmpty());

        pageableInfo.setSort(sortInfo);

        pageableResponse.setPageable(pageableInfo);

        return pageableResponse;
    }
}
