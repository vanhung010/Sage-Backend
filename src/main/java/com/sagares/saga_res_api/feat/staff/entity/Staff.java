package com.sagares.saga_res_api.feat.staff.entity;


import com.sagares.saga_res_api.common.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "staffs")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Staff extends BaseEntity {

}
