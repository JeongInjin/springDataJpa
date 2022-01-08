package study.data_querydsl_jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.data_querydsl_jpa.entity.Item;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    public void save() throws Exception {
        //given
        /*
         * @GeneratedValue 형식이 아니면 상당히 비 효율적이다. -> merge 형식의 전략으로 진행되었다가, new 형식으로 다시 진행된다. save 메서드를 따라가보자.
         *      해당 key 값으로 적용 및 진행 하고싶으면 Entity에 persistable<T> 형식으로 인터페이스를 상속 받아야 한다.
         *      createdDate 형식으로 isNew 값을 true 형식으로 전략화 한다.
         * */
        Item item = new Item("A");
        itemRepository.save(item);
        //when

        //then
    }

}