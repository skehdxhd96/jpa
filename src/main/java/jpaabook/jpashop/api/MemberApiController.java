package jpaabook.jpashop.api;

import jpaabook.jpashop.domain.Address;
import jpaabook.jpashop.domain.Member;
import jpaabook.jpashop.domain.service.MemberService;
import jpaabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

//responsebody : data 자체를 xml이나 json으로 보내버림.
@RestController // controller + responsebody
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
//        return memberService.findMembers();
        return memberRepository.findAll();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDTO> collect = findMembers.stream()
                .map(m -> new MemberDTO(m.getName(), m.getAddress()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data // 이게 뭐지
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDTO<T> {
        private String name;
        private Address address;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {

        //변수로 Member 라는 특정 Entity를 받았다가는 난리난다.
        //api라는것은 기본적으로 한군데가 아닌 다양한 곳에서 호출 할 수 있는 것.
        //그런데 변수로 엔티티 자체를 받아버렸다가는 서버쪽에서 Member라는 엔티티의 변수명을 바꿔버리게 되면, api의 스펙 자체가 변하여 api가 깨지게 된다.
        //이럴 때는 변수로 엔티티가 아닌 api전용 객체인 DTO를 만들어 사용하는것이 합리적이다.
        //엔티티를 웹에 노출해서도 안된다.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {

        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        public CreateMemberResponse(Long id) {
            this.id = id;
        }

        private Long id;
    }
}
