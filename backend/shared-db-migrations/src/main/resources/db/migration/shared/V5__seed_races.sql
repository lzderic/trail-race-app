-- Seed sample races so the application is usable out of the box
INSERT INTO races (id, name, distance)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Mountain Trail 5K',    'FIVE_K'),
    ('b0000000-0000-0000-0000-000000000002', 'Forest Loop 10K',     'TEN_K'),
    ('b0000000-0000-0000-0000-000000000003', 'Coastal Half Marathon','HALF_MARATHON'),
    ('b0000000-0000-0000-0000-000000000004', 'Alpine Marathon',      'MARATHON'),
    ('b0000000-0000-0000-0000-000000000005', 'City Park 5K',         'FIVE_K'),
    ('b0000000-0000-0000-0000-000000000006', 'River Valley 10K',     'TEN_K')
ON CONFLICT (id) DO NOTHING;

