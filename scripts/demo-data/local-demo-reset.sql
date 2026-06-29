-- Local/demo reset only. This script intentionally avoids Flyway so destructive
-- data cleanup cannot run automatically in a shared or production-like database.

BEGIN;

DELETE FROM file_schema.file_metadata;

DELETE FROM notification_schema.email_deliveries;
DELETE FROM notification_schema.notifications;

DELETE FROM workflow_schema.outbox_events;
DELETE FROM workflow_schema.sla_ticket_states;

DELETE FROM reporting_schema.agent_worklog_projection;
DELETE FROM reporting_schema.sla_compliance_daily_projection;
DELETE FROM reporting_schema.agent_performance_daily_projection;
DELETE FROM reporting_schema.ticket_status_daily_projection;
DELETE FROM reporting_schema.ticket_report_projection;

DELETE FROM ticket_schema.ticket_conversation_reads;
DELETE FROM ticket_schema.ticket_worklogs;
DELETE FROM ticket_schema.ticket_comments;
DELETE FROM ticket_schema.outbox_events;
DELETE FROM ticket_schema.tickets;

DELETE FROM ticket_schema.team_members
WHERE actor_id NOT IN (
  '30000000-0000-0000-0000-000000000001',
  '30000000-0000-0000-0000-000000000002',
  '30000000-0000-0000-0000-000000000003',
  '30000000-0000-0000-0000-000000000004',
  '30000000-0000-0000-0000-000000000005',
  '30000000-0000-0000-0000-000000000006',
  '30000000-0000-0000-0000-000000000007',
  '30000000-0000-0000-0000-000000000008',
  '40000000-0000-0000-0000-000000000001',
  '40000000-0000-0000-0000-000000000002',
  '40000000-0000-0000-0000-000000000003',
  '40000000-0000-0000-0000-000000000004',
  '40000000-0000-0000-0000-000000000005',
  '40000000-0000-0000-0000-000000000006',
  '40000000-0000-0000-0000-000000000007',
  '40000000-0000-0000-0000-000000000008'
);

CREATE TEMP TABLE demo_agent_seed (
  agent_index INTEGER PRIMARY KEY,
  agent_id UUID NOT NULL,
  team_id UUID NOT NULL,
  department_id UUID NOT NULL,
  topic_id UUID NOT NULL,
  product_id UUID NOT NULL,
  topic_code VARCHAR(80) NOT NULL,
  topic_name VARCHAR(160) NOT NULL,
  department_code VARCHAR(80) NOT NULL,
  department_name VARCHAR(160) NOT NULL,
  summary VARCHAR(180) NOT NULL,
  description VARCHAR(5000) NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_agent_seed (
  agent_index,
  agent_id,
  team_id,
  department_id,
  topic_id,
  product_id,
  topic_code,
  topic_name,
  department_code,
  department_name,
  summary,
  description
)
VALUES
  (
    1,
    '40000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    '60000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'PASSWORD_RESET',
    'Password Reset',
    'ACCESS_MANAGEMENT',
    'Access Management',
    'Hesap sifresi sifirlanamiyor',
    'Kullanici sifre yenileme adiminda e-posta dogrulamasini tamamlayamiyor.'
  ),
  (
    2,
    '40000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000001',
    '60000000-0000-0000-0000-000000000002',
    '11111111-1111-1111-1111-111111111111',
    'PERMISSION_REQUEST',
    'Permission Request',
    'ACCESS_MANAGEMENT',
    'Access Management',
    'Yetki talebi onay bekliyor',
    'Yeni gorev icin gerekli rol ve erisim kapsamlarinin tanimlanmasi gerekiyor.'
  ),
  (
    3,
    '40000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000002',
    '60000000-0000-0000-0000-000000000003',
    '11111111-1111-1111-1111-111111111111',
    'WEB_PORTAL_BUG',
    'Web Portal Bug',
    'APPLICATION_SUPPORT',
    'Application Support',
    'Portal sayfasi hata veriyor',
    'Musteri portalinda listeleme ekrani belirli filtrelerde hata mesaji gosteriyor.'
  ),
  (
    4,
    '40000000-0000-0000-0000-000000000004',
    '20000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000002',
    '60000000-0000-0000-0000-000000000004',
    '33333333-3333-3333-3333-333333333333',
    'CORE_SYSTEM_ERROR',
    'Core System Error',
    'APPLICATION_SUPPORT',
    'Application Support',
    'Core servis zaman asimi',
    'Backend islem kuyrugunda gecikme ve zaman asimi uyarilari goruluyor.'
  ),
  (
    5,
    '40000000-0000-0000-0000-000000000005',
    '20000000-0000-0000-0000-000000000005',
    '10000000-0000-0000-0000-000000000003',
    '60000000-0000-0000-0000-000000000005',
    '33333333-3333-3333-3333-333333333333',
    'NETWORK_CONNECTIVITY',
    'Network Connectivity',
    'INFRASTRUCTURE',
    'Infrastructure',
    'VPN baglantisi kopuyor',
    'Kurumsal VPN baglantisi yogun saatlerde tekrar eden kopmalar yasiyor.'
  ),
  (
    6,
    '40000000-0000-0000-0000-000000000006',
    '20000000-0000-0000-0000-000000000006',
    '10000000-0000-0000-0000-000000000003',
    '60000000-0000-0000-0000-000000000006',
    '33333333-3333-3333-3333-333333333333',
    'SERVER_PLATFORM',
    'Server Platform',
    'INFRASTRUCTURE',
    'Infrastructure',
    'Ortam kaynak kullanimi yuksek',
    'Uygulama sunucusunda CPU ve bellek kullanim metrikleri normalin uzerinde.'
  ),
  (
    7,
    '40000000-0000-0000-0000-000000000007',
    '20000000-0000-0000-0000-000000000007',
    '10000000-0000-0000-0000-000000000004',
    '60000000-0000-0000-0000-000000000007',
    '11111111-1111-1111-1111-111111111111',
    'INVOICE_ISSUE',
    'Invoice Issue',
    'FINANCE_OPERATIONS',
    'Finance Operations',
    'Fatura bilgisi hatali',
    'Musteri faturadaki adres ve vergi bilgisi alanlarinin duzeltilmesini istiyor.'
  ),
  (
    8,
    '40000000-0000-0000-0000-000000000008',
    '20000000-0000-0000-0000-000000000008',
    '10000000-0000-0000-0000-000000000004',
    '60000000-0000-0000-0000-000000000008',
    '22222222-2222-2222-2222-222222222222',
    'PAYMENT_FAILURE',
    'Payment Failure',
    'FINANCE_OPERATIONS',
    'Finance Operations',
    'Odeme islemi tamamlanmiyor',
    'Kart ile odeme adiminda islem tamamlanmadan hata mesaji aliniyor.'
  );

DO $$
DECLARE
  demo_customers UUID[] := ARRAY[
    '80000000-0000-0000-0000-000000000001'::UUID,
    '80000000-0000-0000-0000-000000000004'::UUID,
    '80000000-0000-0000-0000-000000000005'::UUID,
    '80000000-0000-0000-0000-000000000006'::UUID,
    '80000000-0000-0000-0000-000000000007'::UUID
  ];
  seed demo_agent_seed%ROWTYPE;
  ticket_index INTEGER;
  sequence_number INTEGER := 0;
  ticket_id UUID;
  ticket_number VARCHAR(32);
  customer_id UUID;
  priority_value VARCHAR(20);
  status_value VARCHAR(40);
  sla_status_value VARCHAR(40);
  opened_at_value TIMESTAMPTZ;
  updated_at_value TIMESTAMPTZ;
  closed_at_value TIMESTAMPTZ;
  target_resolution_value TIMESTAMPTZ;
  worklog_minutes INTEGER;
BEGIN
  FOR seed IN SELECT * FROM demo_agent_seed ORDER BY agent_index LOOP
    FOR ticket_index IN 1..12 LOOP
      sequence_number := sequence_number + 1;
      ticket_id := ('90000000-0000-0000-0000-' || lpad(sequence_number::TEXT, 12, '0'))::UUID;
      ticket_number := 'TCK-' || lpad((1000 + sequence_number)::TEXT, 6, '0');
      customer_id := demo_customers[((sequence_number - 1) % array_length(demo_customers, 1)) + 1];
      priority_value := CASE
        WHEN ticket_index IN (1, 4, 8, 9) THEN 'HIGH'
        WHEN ticket_index IN (2, 5, 10, 11) THEN 'MEDIUM'
        ELSE 'LOW'
      END;
      status_value := CASE
        WHEN ticket_index <= 9 THEN 'CLOSED'
        WHEN ticket_index = 10 THEN 'RESOLVED'
        WHEN ticket_index = 11 THEN 'IN_PROGRESS'
        ELSE 'WAITING_FOR_CUSTOMER'
      END;
      sla_status_value := CASE
        WHEN ticket_index = 9 THEN 'BREACHED'
        WHEN ticket_index <= 10 THEN 'MET'
        WHEN ticket_index = 11 THEN 'ACTIVE'
        ELSE 'AT_RISK'
      END;
      opened_at_value := date_trunc('day', now())
        - make_interval(days => ((seed.agent_index * 3 + ticket_index * 2) % 30) + 1)
        + make_interval(hours => 8 + (ticket_index % 5));
      IF ticket_index = 11 THEN
        opened_at_value := now() - interval '2 hours';
      ELSIF ticket_index = 12 THEN
        opened_at_value := now() - interval '6 hours';
      END IF;
      target_resolution_value := opened_at_value + CASE priority_value
        WHEN 'HIGH' THEN interval '8 hours'
        WHEN 'MEDIUM' THEN interval '24 hours'
        ELSE interval '72 hours'
      END;
      closed_at_value := CASE
        WHEN status_value = 'CLOSED' AND sla_status_value = 'BREACHED' THEN opened_at_value + interval '14 hours'
        WHEN status_value = 'CLOSED' AND priority_value = 'HIGH' THEN opened_at_value + interval '5 hours'
        WHEN status_value = 'CLOSED' AND priority_value = 'MEDIUM' THEN opened_at_value + interval '10 hours'
        WHEN status_value = 'CLOSED' THEN opened_at_value + interval '22 hours'
        ELSE NULL
      END;
      updated_at_value := COALESCE(closed_at_value, opened_at_value + interval '6 hours');
      worklog_minutes := 35 + (seed.agent_index * 4) + (ticket_index * 3);

      INSERT INTO ticket_schema.tickets (
        id,
        ticket_number,
        customer_id,
        product_id,
        summary,
        description,
        priority,
        status,
        created_at,
        updated_at,
        assignee_id,
        assigned_team_id,
        topic_id,
        routed_department_id
      )
      VALUES (
        ticket_id,
        ticket_number,
        customer_id,
        seed.product_id,
        seed.summary || ' #' || ticket_index,
        seed.description,
        priority_value,
        status_value,
        opened_at_value,
        updated_at_value,
        seed.agent_id,
        seed.team_id,
        seed.topic_id,
        seed.department_id
      );

      INSERT INTO ticket_schema.ticket_comments (
        id,
        ticket_id,
        author_id,
        visibility,
        body,
        created_at
      )
      VALUES (
        ('92000000-0000-0000-0000-' || lpad(sequence_number::TEXT, 12, '0'))::UUID,
        ticket_id,
        customer_id,
        'EXTERNAL',
        seed.description,
        opened_at_value + interval '10 minutes'
      );

      IF ticket_index <= 10 THEN
        INSERT INTO ticket_schema.ticket_comments (
          id,
          ticket_id,
          author_id,
          visibility,
          body,
          created_at
        )
        VALUES (
          ('93000000-0000-0000-0000-' || lpad(sequence_number::TEXT, 12, '0'))::UUID,
          ticket_id,
          seed.agent_id,
          'EXTERNAL',
          'Inceleme tamamlandi ve musteriye cozum bilgisi iletildi.',
          updated_at_value - interval '20 minutes'
        );

        INSERT INTO ticket_schema.ticket_worklogs (
          id,
          ticket_id,
          agent_id,
          work_date,
          duration_minutes,
          description,
          created_at
        )
        VALUES (
          ('91000000-0000-0000-0000-' || lpad(sequence_number::TEXT, 12, '0'))::UUID,
          ticket_id,
          seed.agent_id,
          updated_at_value::DATE,
          worklog_minutes,
          'Demo cozum ve dogrulama calismasi.',
          updated_at_value - interval '15 minutes'
        );

        INSERT INTO reporting_schema.agent_worklog_projection (
          worklog_id,
          ticket_id,
          ticket_number,
          agent_id,
          work_date,
          duration_minutes
        )
        VALUES (
          ('91000000-0000-0000-0000-' || lpad(sequence_number::TEXT, 12, '0'))::UUID,
          ticket_id,
          ticket_number,
          seed.agent_id,
          updated_at_value::DATE,
          worklog_minutes
        );
      END IF;

      IF ticket_index IN (2, 5) THEN
        INSERT INTO ticket_schema.ticket_comments (
          id,
          ticket_id,
          author_id,
          visibility,
          body,
          created_at
        )
        VALUES (
          ('94000000-0000-0000-0000-' || lpad(sequence_number::TEXT, 12, '0'))::UUID,
          ticket_id,
          seed.agent_id,
          'INTERNAL',
          'Demo ic not: SLA hedefi ve tekrar acilma riski kontrol edildi.',
          updated_at_value - interval '30 minutes'
        );
      END IF;

      INSERT INTO workflow_schema.sla_ticket_states (
        ticket_id,
        ticket_number,
        priority,
        opened_at,
        target_resolution_at,
        status,
        created_at,
        updated_at,
        customer_id,
        assignee_id,
        assigned_team_id,
        risk_detected_at,
        breached_at
      )
      VALUES (
        ticket_id,
        ticket_number,
        priority_value,
        opened_at_value,
        target_resolution_value,
        sla_status_value,
        opened_at_value,
        updated_at_value,
        customer_id,
        seed.agent_id,
        seed.team_id,
        CASE WHEN sla_status_value IN ('AT_RISK', 'BREACHED') THEN target_resolution_value - interval '2 hours' ELSE NULL END,
        CASE WHEN sla_status_value = 'BREACHED' THEN target_resolution_value + interval '1 hour' ELSE NULL END
      );

      INSERT INTO reporting_schema.ticket_report_projection (
        ticket_id,
        ticket_number,
        customer_id,
        product_id,
        priority,
        status,
        assignee_id,
        assigned_team_id,
        opened_at,
        updated_at,
        closed_at,
        sla_target_resolution_at,
        sla_status,
        projected_at,
        topic_code,
        topic_name,
        routed_department_id,
        routed_department_code,
        routed_department_name
      )
      VALUES (
        ticket_id,
        ticket_number,
        customer_id,
        seed.product_id,
        priority_value,
        status_value,
        seed.agent_id,
        seed.team_id,
        opened_at_value,
        updated_at_value,
        closed_at_value,
        target_resolution_value,
        sla_status_value,
        now(),
        seed.topic_code,
        seed.topic_name,
        seed.department_id,
        seed.department_code,
        seed.department_name
      );
    END LOOP;
  END LOOP;
END $$;

SELECT setval('ticket_schema.ticket_number_seq', 1096, true);

COMMIT;
